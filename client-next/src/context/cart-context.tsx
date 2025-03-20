"use client";

import {
  CustomEntityModel,
  PlanResponse,
  UserCartBody,
  UserCartResponse,
} from "@/types/dto";
import {
  createContext,
  Dispatch,
  ReactNode,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useReducer,
} from "react";
import {
  isDeepEqual,
  roundToDecimalPlaces,
  wrapItemToString,
} from "@/lib/utils";
import { fetchStream } from "@/lib/fetchers/fetchStream";
import { Session } from "next-auth";
import { BaseError } from "@/types/responses";

const initialState: UserCart = {
  plans: [],
  total: 0,
  userId: "",
};
async function loadInitialCart(
  authUser: Session["user"],
): Promise<UserCart | undefined> {
  if (!authUser?.token) {
    return initialState;
  }
  try {
    const { messages, error } = await fetchStream<
      CustomEntityModel<UserCartResponse>
    >({
      method: "POST",
      token: authUser.token,
      path: `/cart/getOrCreate/${authUser.id}`,
    });
    if (error || messages.length == 0) {
      return initialState;
    }
    return {
      plans: messages[0].content.plans,
      total: messages[0].content.plans.length,
      userId: `${messages[0].content.userId}`,
    };
  } catch (err) {
    return undefined;
  }
}
type ObservableCallback<A> = (
  oldState: UserCart,
  userId: string,
  payload: A,
) => void;

type ObservableCallbackWrapper<A> = (
  oldState: UserCart,
  userId: string,
  payload: A,
  dispatch: Dispatch<CartActionWrapper>,
) => void;
export type DispatchCartAction = "ADD" | "REMOVE" | "CLEAR";

export interface UserCart {
  plans: PlanResponse[];
  total: number;
  userId: string;
}

export interface CartState {
  carts: { [userId: string]: UserCart };
}

export interface IdType {
  id: number;
}

const noOpObservableCallback: ObservableCallback<unknown> = (_, __, ___) => {};

function updateOldState(
  oldState: UserCart,
  newState: {
    messages: CustomEntityModel<UserCartResponse>[];
    error: BaseError | null;
  },
  dispatch: Dispatch<CartActionWrapper>,
) {
  // console.log("CART NEW STATE", newState);
  if (newState.error || newState.messages.length === 0) {
    return;
  }
  const content = newState.messages[0].content;
  const parsed: UserCart = {
    plans: content.plans,
    total: content.plans.length,
    userId: `${content.userId}`,
  };

  // console.log(
  //   "EQUAL",
  //   isDeepEqual(oldState.plans, parsed.plans),
  //   oldState.plans,
  //   parsed.plans,
  //   parsed.userId === oldState.userId,
  // );
  const areEqual =
    parsed.userId === oldState.userId &&
    isDeepEqual(oldState.plans, parsed.plans); //todo schimba cu id uri si pret
  if (!areEqual) {
    // console.log(
    //   "CART PARSED",
    //   parsed.plans.map((p) => p.id),
    // );
    // console.log(
    //   "CART OLD STATE",
    //   oldState.plans.map((p) => p.id),
    // );
    dispatch({
      type: "ADD_ALL",
      userId: parsed.userId,
      payload: parsed.plans,
      observableCallback: noOpObservableCallback,
    });
    return;
  }
  return;
}

const addObservableCallback: ObservableCallbackWrapper<PlanResponse> = (
  oldState,
  userId,
  payload,
  dispatch,
) => {
  const body: UserCartBody = {
    planIds: [payload.id],
  };
  fetchStream<CustomEntityModel<UserCartResponse>>({
    method: "PUT",
    path: `/cart/add/${userId}`,
    body,
  })
    .then((r) => updateOldState(oldState, r, dispatch))
    .catch((e) => console.error("Failed to update cart", e));
};
const removeObservableCallback: ObservableCallbackWrapper<IdType> = (
  oldState,
  userId,
  payload,
  dispatch,
) => {
  const body: UserCartBody = {
    planIds: [payload.id],
  };
  fetchStream<CustomEntityModel<UserCartResponse>>({
    method: "PATCH",
    path: `/cart/remove/${userId}`,
    body,
  })
    .then((r) => updateOldState(oldState, r, dispatch))
    .catch((e) => console.error("Failed to update cart", e));
};
const clearObservableCallback: ObservableCallbackWrapper<string> = (
  oldState,
  userId,
  payload,
  dispatch,
) => {
  fetchStream<CustomEntityModel<UserCartResponse>>({
    method: "DELETE",
    path: `/cart/deleteCreateNew/${userId}`,
  })
    .then((r) => {
      console.log("CLEAR OBSERVABLE", r);
      if (r.error || r.messages.length === 0) {
        return;
      }
      const content = r.messages[0].content;
      const parsed: UserCart = {
        plans: content.plans,
        total: content.plans.length,
        userId: `${content.userId}`,
      };
      if (oldState.userId !== "") {
        dispatch({
          type: "ADD_ALL",
          userId: parsed.userId,
          payload: parsed.plans,
          observableCallback: noOpObservableCallback,
        });
      }
    })
    .catch((e) => console.error("Failed to update cart", e));
};

const addAllObservableCallback: ObservableCallbackWrapper<PlanResponse[]> = (
  oldState,
  userId,
  payload,
  dispatch,
) => {
  const body: UserCartBody = {
    planIds: payload.map((p) => p.id),
  };
  fetchStream<CustomEntityModel<UserCartResponse>>({
    method: "PUT",
    path: `/cart/add/${userId}`,
    body,
  })
    .then((r) => updateOldState(oldState, r, dispatch))
    .catch((e) => console.error("Failed to update cart", e));
};

const observableActions: Record<
  "ADD" | "CLEAR" | "REMOVE" | "ADD_ALL",
  ObservableCallbackWrapper<any>
> = {
  ADD: addObservableCallback,
  REMOVE: removeObservableCallback,
  CLEAR: clearObservableCallback,
  ADD_ALL: addAllObservableCallback,
} as const;

export type CartAction =
  | { type: "ADD" | "CLEAR"; userId: string; payload?: PlanResponse }
  | { type: "REMOVE"; userId: string; payload: IdType }
  | { type: "ADD_ALL"; userId: string; payload: PlanResponse[] };

export type CartActionWrapper = CartAction & {
  observableCallback: ObservableCallback<unknown>;
};

export const cartReducer = (
  state: UserCart | undefined,
  action: CartActionWrapper,
): UserCart => {
  const { userId } = action;
  const userCart = state || { plans: [], total: 0, userId };
  // console.log("ACTION", action);

  switch (action.type) {
    case "ADD":
      if (!action.payload) return userCart;
      const planExists = userCart.plans.find(
        ({ id }) => id === action?.payload?.id,
      );
      if (planExists) return userCart;
      const updatedAddCart: UserCart = {
        ...userCart,
        plans: [...userCart.plans, action.payload],
        total: userCart.total + 1,
        userId,
      };
      action.observableCallback(updatedAddCart, userId, action.payload);
      return updatedAddCart;

    case "REMOVE":
      if (!action.payload) return userCart;
      const filteredPlans = userCart.plans.filter(
        ({ id }) => id !== action?.payload?.id,
      );
      const removeState = {
        plans: filteredPlans,
        total: filteredPlans.length,
        userId,
      };
      action.observableCallback(removeState, userId, action.payload);
      return removeState;

    case "CLEAR":
      const clearState = {
        plans: [],
        total: 0,
        userId,
      };
      const observablePayload = userCart.total !== 0 ? "call" : undefined;
      action.observableCallback(clearState, userId, observablePayload);
      return clearState;

    case "ADD_ALL":
      // console.log("CART ADD_ALL ACTION PAYLOAD", action.payload);
      if (!action.payload) return userCart;
      const addAllState = {
        plans: action.payload,
        total: action.payload.length,
        userId,
      };
      action.observableCallback(addAllState, userId, action.payload);
      return addAllState;

    default:
      return userCart;
  }
};

export interface CartContextType {
  state: UserCart;
  dispatch: Dispatch<CartAction>;
}

export const CartContext = createContext<CartContextType | null>(null);

interface Props {
  children: ReactNode;
  authUser: Session["user"];
}

export const CartProvider = ({ children, authUser }: Props) => {
  const authUserId = authUser?.id;
  const [state, dispatch] = useReducer(cartReducer, {
    plans: [],
    total: 0,
    userId: authUserId ? `${authUserId}` : "",
  });

  const dispatchWrapper = useCallback((action: CartAction) => {
    dispatch({
      ...action,
      observableCallback: (oldState, userId, payload) => {
        if (payload) {
          observableActions[action.type](oldState, userId, payload, dispatch);
        }
      },
    });
  }, []);

  // console.log("CART STATE CTX", state);

  useEffect(() => {
    if (authUserId !== "") {
      loadInitialCart(authUser)
        .then((cart) => {
          console.log("INITIAL CART", cart);
          if (cart) {
            // todo verifica daca state userid e ''
            dispatch({
              type: "ADD_ALL",
              userId: cart.userId,
              payload: cart.plans,
              observableCallback: noOpObservableCallback,
            });
          }
        })
        .catch((e) => {
          console.error("Failed to load cart from local storage:", e);
        });
    }
  }, [authUserId]);

  return (
    <CartContext.Provider value={{ state, dispatch: dispatchWrapper }}>
      {children}
    </CartContext.Provider>
  );
};

//todo reverfica tot si dupa scoate wrapItemToString

export const useCart = () => {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error("useCart must be used within a CartProvider");
  }

  const addToCart = useCallback(
    (userId: string, plan: PlanResponse) =>
      context.dispatch({
        type: "ADD",
        userId: wrapItemToString(userId),
        payload: plan,
      }),
    [context],
  );

  const removeFromCart = useCallback(
    (userId: string, id: IdType) =>
      context.dispatch({
        type: "REMOVE",
        userId: wrapItemToString(userId),
        payload: id,
      }),
    [context],
  );

  const clearCart = useCallback(
    (userId: string) =>
      context.dispatch({ type: "CLEAR", userId: wrapItemToString(userId) }),
    [context],
  );

  const getCartForUser = useCallback(
    (userId: string): UserCart => {
      // console.log(
      //   "CART STATE CONTEXT",
      //   context.state,
      //   context.state && context.state.userId === userId,
      //   context.state.userId,
      //   userId,
      // );
      return context.state &&
        wrapItemToString(context.state.userId) === wrapItemToString(userId)
        ? context.state
        : { plans: [], total: 0, userId: "" };
    },
    [context.state],
  );

  const getPlanIdsForUser = useCallback(
    (userId: string) => getCartForUser(userId).plans.map(({ id }) => id),
    [getCartForUser],
  );

  const addAllForUser = useCallback(
    (userId: string, plans: PlanResponse[]) =>
      context.dispatch({
        type: "ADD_ALL",
        userId: wrapItemToString(userId),
        payload: plans,
      }),
    [context],
  );

  const cartTotalPrice = useCallback(
    (userId: string) =>
      roundToDecimalPlaces(
        getCartForUser(userId).plans.reduce((acc, { price }) => acc + price, 0),
        2,
      ) || 0,
    [getCartForUser],
  );

  const isInCart = useCallback(
    (userId: string, id: IdType) =>
      getCartForUser(userId).plans.some((plan) => plan.id === id.id),

    [getCartForUser],
  );

  return {
    getCartForUser,
    addToCart,
    removeFromCart,
    clearCart,
    isInCart,
    cartTotalPrice,
    getPlanIdsForUser,
    addAllForUser,
  };
};

export const useCartForUser = (userId: string) => {
  const {
    addToCart,
    clearCart,
    getCartForUser,
    removeFromCart,
    isInCart,
    cartTotalPrice,
    getPlanIdsForUser,
    addAllForUser,
  } = useCart();

  const usersCart = useMemo(
    () => getCartForUser(userId),
    [getCartForUser, userId],
  );

  const addToCartForUser = useCallback(
    (plan: PlanResponse) => addToCart(userId, plan),
    [addToCart, userId],
  );

  const removeFromCartForUser = useCallback(
    (id: IdType) => removeFromCart(userId, id),
    [removeFromCart, userId],
  );

  const clearCartForUser = useCallback(
    () => clearCart(userId),
    [clearCart, userId],
  );
  const isInCartForUser = useCallback(
    (id: IdType) => isInCart(userId, id),
    [isInCart, userId],
  );

  const usersCartTotalPrice = useMemo(
    () => cartTotalPrice(userId),
    [cartTotalPrice, userId],
  );

  const usersPlanIds = useMemo(
    () => getPlanIdsForUser(userId),
    [getPlanIdsForUser, userId],
  );
  const usersAddAll = useCallback(
    (plans: PlanResponse[]) => addAllForUser(userId, plans),
    [addAllForUser, userId],
  );
  return {
    usersCart,
    addToCartForUser,
    removeFromCartForUser,
    clearCartForUser,
    isInCartForUser,
    usersCartTotalPrice,
    usersPlanIds,
    usersAddAll,
  };
};
