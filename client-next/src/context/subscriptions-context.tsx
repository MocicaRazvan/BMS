"use client";
import {
  createContext,
  Dispatch,
  ReactNode,
  useCallback,
  useContext,
  useEffect,
  useReducer,
} from "react";
import { Session } from "next-auth";
import { fetchStream } from "@/hoooks/fetchStream";
import { UserSubscriptionDto } from "@/types/dto";

export interface UserSubscriptions {
  planIds: number[];
}

export type SubscriptionAction =
  | { type: "ADD"; payload: number }
  | { type: "INIT_ALL"; payload: number[] };

const initialState: UserSubscriptions = {
  planIds: [],
};

export const subscriptionReducer = (
  state: UserSubscriptions,
  action: SubscriptionAction,
): UserSubscriptions => {
  switch (action.type) {
    case "ADD":
      return {
        ...state,
        planIds: [...new Set([...state.planIds, action.payload])],
      };
    case "INIT_ALL":
      return {
        ...state,
        planIds: action.payload,
      };

    default:
      return state;
  }
};

export interface SubscriptionContextType {
  subscriptions: UserSubscriptions;
  dispatch: Dispatch<SubscriptionAction>;
}

export const SubscriptionContext =
  createContext<SubscriptionContextType | null>(null);

interface Props {
  children: ReactNode;
  authUser: Session["user"];
}

export const SubscriptionProvider = ({ children, authUser }: Props) => {
  const [state, dispatch] = useReducer(subscriptionReducer, initialState);

  useEffect(() => {
    if (authUser && authUser?.token) {
      fetchStream<UserSubscriptionDto>({
        path: "/orders/subscriptions",
        token: authUser.token,
        successCallback: (data) => {
          console.log("Subscription fetch success", data);
          dispatch({ type: "ADD", payload: data.planId });
        },
      }).catch((e) => {
        console.error("Subscription fetch error", e);
      });
    }
  }, [JSON.stringify(authUser)]);

  console.log("Subscriptions", state);

  return (
    <SubscriptionContext.Provider value={{ subscriptions: state, dispatch }}>
      {children}
    </SubscriptionContext.Provider>
  );
};

export const useSubscription = () => {
  const context = useContext(SubscriptionContext);
  if (!context) {
    throw new Error(
      "useSubscription must be used within a SubscriptionProvider",
    );
  }

  const isPlanInSubscription = useCallback(
    (planId: number) => context.subscriptions.planIds.includes(planId),
    [context.subscriptions.planIds],
  );

  const getSubscriptionPlanIds = useCallback(
    () => context.subscriptions.planIds,
    [context.subscriptions.planIds],
  );

  return { isPlanInSubscription, getSubscriptionPlanIds };
};
