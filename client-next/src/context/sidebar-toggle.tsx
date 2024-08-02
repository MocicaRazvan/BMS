"use client";
import {
  createContext,
  ReactNode,
  useCallback,
  useContext,
  useState,
} from "react";

export interface SidebarToggleState {
  isOpen: boolean;
  toggleIsOpen: () => void;
}

export const initialState: SidebarToggleState = {
  isOpen: false,
  toggleIsOpen: () => {
    return;
  },
};

export const SidebarToggleContext = createContext<SidebarToggleState | null>(
  null,
);

interface Props {
  children: ReactNode;
}

export const SidebarToggleProvider = ({ children }: Props) => {
  const [isOpen, setIsOpen] = useState<boolean>(initialState.isOpen);

  const toggleIsOpen = useCallback(
    () => setIsOpen((prevState) => !prevState),
    [],
  );

  return (
    <SidebarToggleContext.Provider
      value={{
        isOpen,
        toggleIsOpen,
      }}
    >
      {children}
    </SidebarToggleContext.Provider>
  );
};

export const useSidebarToggle = () => {
  const context = useContext(SidebarToggleContext);
  if (context === null) {
    throw new Error(
      "useSidebarToggle must be used within a SidebarToggleProvider",
    );
  }
  return context;
};
