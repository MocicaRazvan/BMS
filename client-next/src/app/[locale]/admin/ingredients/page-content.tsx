"use client";

import IngredientsTable, {
  IngredientTableProps,
} from "@/components/table/ingredients-table";
import { useSidebarToggle } from "@/context/sidebar-toggle";

export default function AdminIngredientsCreatePageContent(
  props: IngredientTableProps,
) {
  const { isOpen } = useSidebarToggle();

  return <IngredientsTable {...props} isSidebarOpen={isOpen} />;
}
