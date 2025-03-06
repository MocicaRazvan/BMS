import { getCsrfNextAuthHeader } from "@/actions/get-csr-next-auth";
import { OverallSummary } from "@/types/dto";
import HomeAbout, { HomeAboutTexts } from "@/components/home/home-about";
const springUrl = process.env.NEXT_PUBLIC_SPRING!;

interface Props {
  texts: HomeAboutTexts;
}
export default async function HomeAboutWrapper({ texts }: Props) {
  const csrfHeader = await getCsrfNextAuthHeader();

  const res = await fetch(springUrl + "/orders/overall", {
    next: { revalidate: 600 },
    headers: {
      ...csrfHeader,
    },
  });
  if (!res.ok) {
    console.log(res);
    return;
    // throw new Error("Failed to fetch");
  }
  const data = (await res.json()) as OverallSummary;
  // console.log(data);

  return <HomeAbout {...texts} overallSummary={data} />;
}
