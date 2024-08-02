import { getUser } from "@/lib/user";
import { getAdminEmailTexts } from "@/texts/components/forms";
import AdminEmail from "@/components/forms/admin-email";
import KanbanBoard from "@/components/kanban/kanban-board";
import KanbanBoardWrapper from "@/components/kanban/kanban-board-wrapper";
import { getKanbanBoardTexts } from "@/texts/components/kanban";

export default async function TestPage() {
  const [authUser, texts] = await Promise.all([
    getUser(),
    getKanbanBoardTexts(),
  ]);

  return (
    <div>
      <KanbanBoardWrapper authUser={authUser} {...texts} />
    </div>
  );
}
