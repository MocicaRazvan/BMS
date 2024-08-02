"use client";
import { useSidebarToggle } from "@/context/sidebar-toggle";
import { cn } from "@/lib/utils";
import { Link } from "@/navigation";
import { Button } from "@/components/ui/button";
import SidebarToggle from "@/components/admin/sidebar-toggle";
import { Home, PanelsTopLeft } from "lucide-react";
import { Menu } from "@/components/admin/menu";
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import { AdminMenuTexts } from "@/components/admin/menu-list";
import Logo from "@/components/logo/logo";
interface Props {
  menuTexts: AdminMenuTexts;
  mainSite: string;
}

export default function SideBar({ menuTexts, mainSite }: Props) {
  const { isOpen, toggleIsOpen } = useSidebarToggle();

  return (
    <aside
      className={cn(
        "fixed top-0 left-0 z-20 h-screen -translate-x-full lg:translate-x-0 transition-[width] ease-in-out duration-300",
        !isOpen ? "w-[90px] bg-background" : "w-72 bg-muted/20",
      )}
    >
      <SidebarToggle />
      <div className="relative h-full flex flex-col px-3 py-4 overflow-y-auto shadow-md dark:shadow-zinc-800">
        <TooltipProvider disableHoverableContent>
          <Tooltip delayDuration={100}>
            <TooltipTrigger asChild>
              <Button
                className={cn(
                  "transition-transform ease-in-out duration-300 mb-1",
                  !isOpen ? "translate-x-1" : "translate-x-0",
                )}
                variant="link"
                asChild
              >
                <Link
                  href="/"
                  className="flex items-center gap-2 transition-all hover:scale-[1.02]"
                >
                  <div className="me-1">
                    <Logo />
                  </div>
                  <h1
                    className={cn(
                      "font-bold text-lg whitespace-nowrap transition-[transform,opacity,display] " +
                        "ease-in-out duration-300 overflow-x-hidden capitalize",
                      !isOpen
                        ? "-translate-x-96 opacity-0 hidden"
                        : "translate-x-0 opacity-100",
                    )}
                  >
                    {mainSite}
                  </h1>
                </Link>
              </Button>
            </TooltipTrigger>
            {!isOpen && (
              <TooltipContent side="bottom" className={"capitalize"}>
                {mainSite}
              </TooltipContent>
            )}
          </Tooltip>
        </TooltipProvider>

        <Menu isOpen={isOpen} texts={menuTexts} />
      </div>
    </aside>
  );
}
