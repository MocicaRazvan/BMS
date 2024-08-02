"use client";

import { PlanResponse } from "@/types/dto";
import {
  Dispatch,
  ReactNode,
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useReducer,
} from "react";
import { roundToDecimalPlaces } from "@/lib/utils";

const CART_STORAGE_KEY = "userCarts";

function saveCartToLocalStorage(cart: CartState) {
  if (typeof window !== "undefined" && window.localStorage) {
    try {
      localStorage[CART_STORAGE_KEY] = JSON.stringify(cart);
    } catch (err) {
      console.error("Failed to save cart to local storage:", err);
    }
  }
}

function loadCartFromLocalStorage(): CartState | undefined {
  try {
    const serializedCart = localStorage[CART_STORAGE_KEY];
    if (serializedCart === null) return undefined;
    return JSON.parse(serializedCart);
  } catch (err) {
    console.error("Failed to load cart from local storage:", err);
    return undefined;
  }
}

export type DispatchCartAction = "ADD" | "REMOVE" | "CLEAR";

export interface UserCart {
  plans: PlanResponse[];
  total: number;
}

export interface CartState {
  carts: { [userId: string]: UserCart };
}

export interface IdType {
  id: number;
}

export type CartAction =
  | { type: "ADD" | "CLEAR"; userId: string; payload?: PlanResponse }
  | { type: "REMOVE"; userId: string; payload: IdType };

const initialState: CartState = {
  carts: {},
};
export const cartReducer = (
  state: CartState,
  action: CartAction,
): CartState => {
  const { userId } = action;
  const userCart = state.carts[userId] || { plans: [], total: 0 };

  switch (action.type) {
    case "ADD":
      if (!action.payload) return state;
      const planExists = userCart.plans.find(
        ({ id }) => id === action?.payload?.id,
      );
      if (planExists) return state;
      const updatedAddCart: UserCart = {
        ...userCart,
        plans: [...userCart.plans, action.payload],
        total: userCart.total + 1,
      };
      return {
        ...state,
        carts: { ...state.carts, [userId]: updatedAddCart },
      };

    case "REMOVE":
      if (!action.payload) return state;
      const filteredPlans = userCart.plans.filter(
        ({ id }) => id !== action?.payload?.id,
      );
      return {
        ...state,
        carts: {
          ...state.carts,
          [userId]: {
            plans: filteredPlans,
            total: filteredPlans.length,
          },
        },
      };

    case "CLEAR":
      return {
        ...state,
        carts: { ...state.carts, [userId]: { plans: [], total: 0 } },
      };

    default:
      return state;
  }
};

export interface CartContextType {
  state: CartState;
  dispatch: Dispatch<CartAction>;
}

export const CartContext = createContext<CartContextType | null>(null);

interface Props {
  children: ReactNode;
}

export const CartProvider = ({ children }: Props) => {
  const [state, dispatch] = useReducer(
    cartReducer,
    loadCartFromLocalStorage() || initialState,
  );

  useEffect(() => {
    saveCartToLocalStorage(state);
  }, [state]);

  return (
    <CartContext.Provider value={{ state, dispatch }}>
      {children}
    </CartContext.Provider>
  );
};

export const useCart = () => {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error("useCart must be used within a CartProvider");
  }

  const addToCart = useCallback(
    (userId: string, plan: PlanResponse) =>
      context.dispatch({ type: "ADD", userId, payload: plan }),
    [context],
  );

  const removeFromCart = useCallback(
    (userId: string, id: IdType) =>
      context.dispatch({ type: "REMOVE", userId, payload: id }),
    [context],
  );

  const clearCart = useCallback(
    (userId: string) => context.dispatch({ type: "CLEAR", userId }),
    [context],
  );

  const getCartForUser = useCallback(
    (userId: string): UserCart => {
      return context.state.carts[userId] || { plans: [], total: 0 };
    },
    [context.state.carts],
  );

  const getPlanIdsForUser = useCallback(
    (userId: string) => getCartForUser(userId).plans.map(({ id }) => id),
    [getCartForUser],
  );

  const cartTotalPrice = useCallback(
    (userId: string) =>
      roundToDecimalPlaces(
        context.state.carts[userId]?.plans.reduce(
          (acc, { price }) => acc + price,
          0,
        ),
        2,
      ) || 0,
    [context.state.carts],
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
  return {
    usersCart,
    addToCartForUser,
    removeFromCartForUser,
    clearCartForUser,
    isInCartForUser,
    usersCartTotalPrice,
    usersPlanIds,
  };
};
