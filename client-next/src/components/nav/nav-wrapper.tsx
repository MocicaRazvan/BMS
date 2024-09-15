import { getCartPopsTexts, getNavTexts } from "@/texts/components/nav";
import Nav from "@/components/nav/nav";

export default async function NavWrapper() {
  const [navTexts, cartPopTexts] = await Promise.all([
    getNavTexts(),
    getCartPopsTexts(),
  ]);
  return <Nav {...navTexts} cartPopTexts={cartPopTexts} />;
}
