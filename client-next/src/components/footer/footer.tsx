import { Copyright, Home, MailIcon, MapPinIcon, PhoneIcon } from "lucide-react";
import { Link } from "@/navigation";
import Logo from "@/components/logo/logo";
import { getFooterTexts } from "@/texts/components/footer";

export interface FooterTexts {
  home: string;
  title: string;
  plans: string;
  posts: string;
  signUp: string;
  signIn: string;
  termsOfService: string;
  privacyPolicy: string;
  disclaimer: string;
  rightsReserved: string;
}

export default async function Footer() {
  const {
    disclaimer,
    plans,
    home,
    rightsReserved,
    termsOfService,
    privacyPolicy,
    signIn,
    signUp,
    posts,
    title,
  } = await getFooterTexts();
  return (
    <footer className="w-full py-6 mt-28 flex items-center justify-center flex-col ">
      <div className=" max-w-[1600px] mx-auto w-full flex flex-col items-stretch justify-center gap-4 px-4 md:px-6 ">
        <div className="grid items-start gap-10 grid-cols-2 md:gap-4 lg:grid-cols-4 lg:gap-8 ">
          <div className="flex items-center gap-2 col-span-1 transition-all hover:scale-[1.02] hover:underline hover:underline-offset-[2px] md:ps-3">
            <Link className="flex items-center gap-2 font-medium" href="/">
              <Logo />
              <span className="sr-only">{home}</span>
              <p className="text-sm text-gray-500 dark:text-gray-400">
                {title}
              </p>
            </Link>
          </div>
          <div className="space-y-2 col-span-1 capitalize">
            <ul className="grid grid-cols-2 gap-2">
              <li className="transition-all hover:scale-[1.02]">
                <Link className="text-sm font-medium" href="/plans/approved">
                  {plans}
                </Link>
              </li>
              <li className="transition-all hover:scale-[1.02]">
                <Link className="text-sm font-medium" href="/posts/approved">
                  {posts}
                </Link>
              </li>
              <li className="transition-all hover:scale-[1.02]">
                <Link className="text-sm font-medium" href="/auth/signup">
                  {signUp}
                </Link>
              </li>
              <li className="transition-all hover:scale-[1.02]">
                <Link className="text-sm font-medium" href="/auth/signin">
                  {signIn}
                </Link>
              </li>
            </ul>
          </div>
          <div className="space-y-2 col-span-1">
            <div className="flex items-center gap-2">
              <PhoneIcon className="w-4 h-4 flex-shrink-0" />
              <p className="text-sm font-medium">{"0764105200"}</p>
            </div>
            <div className="flex items-center gap-2">
              <MailIcon className="w-4 h-4 flex-shrink-0" />
              <a
                className="text-xs sm:text-sm font-medium underline underline-offset-2"
                href="mailto:razvanmocica@gmail.com"
              >
                {"razvanmocica@gmail.com"}
              </a>
            </div>
          </div>
          <div className="space-y-2 col-span-1 capitalize">
            <div className="transition-all hover:scale-[1.02]">
              <Link className="text-sm font-medium " href="/terms-of-service">
                {termsOfService}
              </Link>
            </div>
            <div className="transition-all hover:scale-[1.02]">
              <Link className="text-sm font-medium" href="#">
                {privacyPolicy}
              </Link>
            </div>
            <div className="transition-all hover:scale-[1.02]">
              <Link className="text-sm font-medium" href="#">
                {disclaimer}
              </Link>
            </div>
          </div>

          <div className="flex flex-col gap-2 col-span-2 w-full lg:col-span-4">
            <hr className="w-full border" />
            <div className="flex items-center justify-start gap-1">
              <Copyright className="w-4 h-4" />
              <p className="text-xs text-gray-500 justify-self-center dark:text-gray-400">
                {new Date().getFullYear()} {rightsReserved}
              </p>
            </div>
          </div>
        </div>
      </div>
    </footer>
  );
}
