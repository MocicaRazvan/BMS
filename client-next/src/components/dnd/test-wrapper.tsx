// import {
//   getImageSchemaTexts,
//   getPostSchema,
//   getPostSchemaTexts,
//   getUpdateProfileSchemaTexts,
// } from "@/types/forms";
// import Test from "@/components/dnd/test";
// import {
//   getInputFileText,
//   getInputMultipleSelectorTexts,
//   getPostFormTexts,
//   getTitleBodyText,
//   getUpdateProfileTexts,
// } from "@/texts/components/forms";
// import PostForm from "@/components/forms/post-form";
// import { getServerSession } from "next-auth";
// import { authOptions } from "@/app/api/auth/[...nextauth]/auth-options";
// import { CustomEntityModel, PostResponse } from "@/types/dto";
// import { Suspense } from "react";
// import LoadingSpinner from "@/components/common/loading-spinner";
// import Image from "next/image";
// import UpdateProfile from "@/components/forms/update-profile";
// import { getUser } from "@/lib/user";
// import Loader from "@/components/ui/spinner";
// import PostsTable from "@/components/table/posts-table";
// import { getPostTableTexts } from "@/texts/components/table";
// import { SortingOption } from "@/components/list/grid-list";
//
// export default async function TestWrapper() {
//   const [postTableTexts] = await Promise.all([getPostTableTexts()]);
//   const sortingOptions: SortingOption[] = [
//     { property: "createdAt", direction: "asc", text: "CreatedAt asc" },
//     { property: "createdAt", direction: "desc", text: "CreatedAt desc" },
//     {
//       property: "title",
//       direction: "asc",
//       text: "title asc",
//     },
//     { property: "title", direction: "desc", text: "title desc" },
//   ];
//   return (
//     <Suspense
//       fallback={
//         <section className="w-full  min-h-[calc(100vh-4rem)] flex-col items-center justify-center transition-all px-6 py-10 relative pb-14 ">
//           <Loader className="w-full" />
//         </section>
//       }
//     >
//       <div className="w-full ">
//         <PostsTable
//           path={`/posts/trainer_/tags/1`}
//           forWhom="trainer_"
//           {...postTableTexts}
//           sortingOptions={sortingOptions}
//           sizeOptions={[5, 10, 20, 30, 40]}
//         />
//       </div>
//     </Suspense>
//   );
// }
