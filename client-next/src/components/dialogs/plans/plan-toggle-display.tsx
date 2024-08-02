import { WithUser } from "@/lib/user";
import { PlanResponse } from "@/types/dto";
import ToggleDisplayDialog from "@/components/dialogs/toggle-display";

interface Props extends WithUser {
  model: PlanResponse;
  callBack: () => void;
}
export default function ToggleDisplayPlan({
  callBack,
  model,
  authUser,
}: Props) {
  return (
    <ToggleDisplayDialog
      model={{ ...model, name: model.title }}
      path={`/plans/alterDisplay/${model.id}`}
      callBack={callBack}
      authUser={authUser}
    />
  );
}
