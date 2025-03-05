"use client";

import { IntlMetadata } from "@/texts/metadata";
import { Role } from "@/types/fetch-utils";
import Fuse, { FuseResult } from "fuse.js";
import { useMemo, useState } from "react";
import { Input } from "@/components/ui/input";
import FindInSite, { FindInSiteTexts } from "@/components/nav/find-in-site";
interface Props {
  metadataValues: {
    metadata: IntlMetadata;
    key: string;
    path: string;
    role: Role | "ROLE_PUBLIC";
  }[];
  texts: FindInSiteTexts;
}
export default function TestPage({ metadataValues, texts }: Props) {
  const fuse = useMemo(
    () =>
      new Fuse(metadataValues, {
        keys: [
          { name: "metadata.title", weight: 0.7 },
          { name: "metadata.description", weight: 0.3 },
          {
            name: "metadata.keywords",
            weight: 0.1,
          },
        ],
        isCaseSensitive: false,
        ignoreDiacritics: true,
        includeScore: true,
        threshold: 0.3,
        useExtendedSearch: true,
      }),
    [metadataValues],
  );
  const [query, setQuery] = useState("");
  const [results, setResults] = useState<Props["metadataValues"]>([]);

  return (
    <div className="w-full mt-20">
      <Input
        value={query}
        onChange={(e) => {
          setQuery(e.target.value);
          if (e.target.value.trim() !== "") {
            const results = fuse
              .search(e.target.value.toLowerCase())
              .map((i) => i.item);
            setResults(results);
          }
        }}
        placeholder="Search"
      />
      {JSON.stringify(results, null, 2)}
      <FindInSite metadataValues={metadataValues} texts={texts} />
    </div>
  );
}
