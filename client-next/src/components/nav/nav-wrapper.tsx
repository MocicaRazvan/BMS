import { getNavTexts } from "@/texts/components/nav";
import Nav from "@/components/nav/nav";

export default async function NavWrapper() {
  const navTexts = await getNavTexts();
  return <Nav {...navTexts} />;
}
