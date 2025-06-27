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
import { fetchStream } from "@/lib/fetchers/fetchStream";
import { UserSubscriptionDto } from "@/types/dto";
import { useSession } from "next-auth/react";

export interface UserSubscriptions {
  planIds: number[];
}

export type SubscriptionAction =
  | { type: "ADD"; payload: number }
  | { type: "INIT_ALL"; payload: number[] }
  | { type: "ADD_ARRAY"; payload: number[] };

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
    case "ADD_ARRAY":
      return {
        ...state,
        planIds: [...new Set([...state.planIds, ...action.payload])],
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
}

export const SubscriptionProvider = ({ children }: Props) => {
  const session = useSession();
  const authUser = session.data?.user;
  const [state, dispatch] = useReducer(subscriptionReducer, initialState);

  useEffect(() => {
    if (!authUser?.token) {
      return;
    }
    const abortController = new AbortController();
    fetchStream<UserSubscriptionDto>({
      path: "/orders/subscriptions",
      token: authUser.token,
      aboveController: abortController,
      // successCallback: (data) => {
      //   console.log("Subscription fetch success", data);
      //   dispatch({ type: "ADD", payload: data.planId });
      // },
      successArrayCallback: (data) => {
        if (abortController.signal.aborted) {
          return;
        }
        dispatch({ type: "ADD_ARRAY", payload: data.map((d) => d.planId) });
      },
    }).catch((e) => {
      console.log("Subscription fetch error", e);
    });
    return () => {
      abortController.abort();
    };
  }, [authUser?.token]);

  return (
    <SubscriptionContext.Provider value={{ subscriptions: state, dispatch }}>
      {children}
    </SubscriptionContext.Provider>
  );
};

export const usePlansSubscription = () => {
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
