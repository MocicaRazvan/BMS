"use client";

import dynamic, { LoaderComponent } from "next/dynamic";
import { ComponentType, createElement, forwardRef, useRef } from "react";
import { DynamicOptions } from "next/dist/shared/lib/dynamic";

export type PreloadComponentType<P = object> = ComponentType<P> & {
  preload: () => Promise<ComponentType<P>>;
};

export type PreloadWrapperType<
  P extends ComponentType<any> = ComponentType<any>,
> = P & {
  preload: () => Promise<ComponentType<any>>;
};

export default function dynamicWithPreload<P extends object = object>(
  loader: () => LoaderComponent<P>,
  options?: Omit<DynamicOptions<P>, "ssr" | "loader">,
) {
  const NextJSDynamicComponent = dynamic(loader, {
    ...options,
    ssr: false,
  });

  let PreloadedComponent: ComponentType<P> | undefined = undefined;
  let loaderPromise: Promise<ComponentType<P>> | undefined = undefined;

  const Component = forwardRef((props, ref) => {
    const ComponentToRender = useRef(
      PreloadedComponent ?? NextJSDynamicComponent,
    );
    return createElement(
      ComponentToRender.current,
      Object.assign(ref ? { ref } : {}, props) as any,
    );
  });
  Component.displayName = NextJSDynamicComponent.displayName + "WithPreload";

  return Object.assign(Component, {
    preload: () => {
      if (!loaderPromise) {
        loaderPromise = loader().then((module) => {
          const comp = "default" in module ? module.default : module;
          PreloadedComponent = comp;
          return comp;
        });
      }
      return loaderPromise;
    },
  }) as PreloadComponentType<P>;
}
