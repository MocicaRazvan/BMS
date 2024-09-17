"use client";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Button } from "@/components/ui/button";
import { Locale, locales, usePathname, useRouter } from "@/navigation";
import { useLocale } from "next-intl";
import { Languages } from "lucide-react";
import { cn } from "@/lib/utils";
import { useCallback, useTransition } from "react";
import { useParams, useSearchParams } from "next/navigation";

interface Props {
  params: { locale: Locale };
}

export function LocaleSwitcher() {
  const router = useRouter();
  const [isPending, startTransition] = useTransition();
  const pathname = usePathname();
  const params = useParams();
  const searchParams = useSearchParams();
  const l = useLocale();

  const handleChange = useCallback(
    (nextLocale: Locale) => {
      // console.log("LocaleSwitcherPathname", pathname);
      // console.log("LocaleSwitcherPathnameTYPE", typeof pathname);
      // console.log("LocaleSwitcherParams", params);
      // console.log("LocaleSwitcherParamsT", typeof params);
      startTransition(() => {
        // router.replace(
        //   // @ts-expect-error -- TypeScript will validate that only known `params`
        //   // are used in combination with a given `pathname`. Since the two will
        //   // always match for the current route, we can skip runtime checks.
        //   { pathname, params },
        //   { locale: nextLocale },
        // );

        // const newParams = new URLSearchParams(searchParams.toString());
        router.push(`${pathname}?${searchParams.toString()}`, {
          locale: nextLocale,
        });
        router.refresh();
      });
    },
    [params, pathname, router],
  );

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="outline" size="icon">
          <div className="flex items-center justify-center w-full">
            <Languages />
          </div>
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent className=" !w-16 !min-w-16 " align="end">
        {locales.map((locale, i) => (
          <div key={locale} className="w-full ">
            <DropdownMenuItem
              className={cn(
                "w-full hover:underline ",
                locale === l && "bg-accent",
                locale !== l && "cursor-pointer",
              )}
              disabled={locale === l}
              onClick={() => handleChange(locale)}
            >
              {locale}
            </DropdownMenuItem>
            {i !== locales.length - 1 && <DropdownMenuSeparator />}
          </div>
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
