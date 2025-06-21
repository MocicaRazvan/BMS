"use client";

import { useCallback, useReducer } from "react";

type State<E> = {
  error: E | null;
  isFinished: boolean;
  isAbsoluteFinished: boolean;
};
type GenericUpdate<T> = T | ((prev: T) => T);
type BooleanUpdater = GenericUpdate<boolean>;
type ErrorUpdater<E> = E | null | ((prev: E | null) => E | null);

type IsFinishedAction = {
  type: "setFinished";
  payload: BooleanUpdater;
};
type IsAbsoluteFinishedAction = {
  type: "setAbsoluteFinished";
  payload: BooleanUpdater;
};
type ErrorAction<E> = {
  type: "setError";
  payload: ErrorUpdater<E>;
};

type Action<E> =
  | { type: "reset" }
  | IsFinishedAction
  | IsAbsoluteFinishedAction
  | ErrorAction<E>
  | {
      type: "setErrorWithFinishes";
      payload: {
        isFinished: BooleanUpdater;
        isAbsoluteFinished: BooleanUpdater;
        error: ErrorUpdater<E>;
      };
    }
  | {
      type: "setFinishes";
      payload: {
        isFinished: BooleanUpdater;
        isAbsoluteFinished: BooleanUpdater;
      };
    };

function isUpdaterFunction<E>(
  payload: (E | null) | ((prev: E | null) => E | null),
): payload is (prev: E | null) => E | null {
  return typeof payload === "function";
}

function errorReducer<E>(
  state: State<E>["error"],
  action: ErrorAction<E>,
): State<E>["error"] {
  switch (action.type) {
    case "setError":
      return isUpdaterFunction(action.payload)
        ? action.payload(state)
        : action.payload;
    default:
      return state;
  }
}

function isFinishedReducer<E>(
  state: State<E>["isFinished"],
  action: IsFinishedAction,
): State<E>["isFinished"] {
  switch (action.type) {
    case "setFinished":
      return typeof action.payload === "function"
        ? action.payload(state)
        : action.payload;
    default:
      return state;
  }
}

function isAbsoluteFinishedReducer<E>(
  state: State<E>["isAbsoluteFinished"],
  action: IsAbsoluteFinishedAction,
): State<E>["isAbsoluteFinished"] {
  switch (action.type) {
    case "setAbsoluteFinished":
      return typeof action.payload === "function"
        ? action.payload(state)
        : action.payload;
    default:
      return state;
  }
}

const getInitialState = <E>(): State<E> => ({
  error: null,
  isFinished: false,
  isAbsoluteFinished: false,
});
function rootReducer<E>(state: State<E>, action: Action<E>): State<E> {
  switch (action.type) {
    case "reset":
      return getInitialState<E>();
    case "setError":
      return {
        ...state,
        error: errorReducer(state.error, action),
      };
    case "setFinished":
      return {
        ...state,
        isFinished: isFinishedReducer(state.isFinished, action),
      };
    case "setAbsoluteFinished":
      return {
        ...state,
        isAbsoluteFinished: isAbsoluteFinishedReducer(
          state.isAbsoluteFinished,
          action,
        ),
      };

    case "setErrorWithFinishes":
      return {
        ...state,
        error: errorReducer(state.error, {
          type: "setError",
          payload: action.payload.error,
        }),
        isFinished: isFinishedReducer(state.isFinished, {
          type: "setFinished",
          payload: action.payload.isFinished,
        }),
        isAbsoluteFinished: isAbsoluteFinishedReducer(
          state.isAbsoluteFinished,
          {
            type: "setAbsoluteFinished",
            payload: action.payload.isAbsoluteFinished,
          },
        ),
      };
    case "setFinishes":
      return {
        ...state,
        isFinished: isFinishedReducer(state.isFinished, {
          type: "setFinished",
          payload: action.payload.isFinished,
        }),
        isAbsoluteFinished: isAbsoluteFinishedReducer(
          state.isAbsoluteFinished,
          {
            type: "setAbsoluteFinished",
            payload: action.payload.isAbsoluteFinished,
          },
        ),
      };
    default:
      return state;
  }
}

export function useAdditionalFetchingReducer<E>() {
  const [{ error, isAbsoluteFinished, isFinished }, dispatch] = useReducer(
    rootReducer<E>,
    undefined,
    getInitialState<E>,
  );

  const setError = useCallback((payload: ErrorAction<E>["payload"]) => {
    dispatch({ type: "setError", payload });
  }, []);

  const setIsFinished = useCallback((payload: IsFinishedAction["payload"]) => {
    dispatch({ type: "setFinished", payload });
  }, []);

  const setIsAbsoluteFinished = useCallback(
    (payload: IsAbsoluteFinishedAction["payload"]) => {
      dispatch({ type: "setAbsoluteFinished", payload });
    },
    [],
  );

  const reset = useCallback(() => {
    dispatch({ type: "reset" });
  }, []);

  const setErrorWithFinishes = useCallback(
    (payload: {
      isFinished: BooleanUpdater;
      isAbsoluteFinished: BooleanUpdater;
      error: ErrorUpdater<E>;
    }) => {
      dispatch({
        type: "setErrorWithFinishes",
        payload,
      });
    },
    [],
  );

  const setFinishes = useCallback(
    (payload: {
      isFinished: BooleanUpdater;
      isAbsoluteFinished: BooleanUpdater;
    }) => {
      dispatch({
        type: "setFinishes",
        payload,
      });
    },
    [],
  );

  return {
    error,
    isAbsoluteFinished,
    isFinished,
    setError,
    setIsFinished,
    setIsAbsoluteFinished,
    reset,
    setErrorWithFinishes,
    setFinishes,
  };
}
