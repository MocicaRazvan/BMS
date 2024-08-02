import { AlertDialogDelete } from "@/components/dialogs/delete-model";
import { PlanResponse } from "@/types/dto";

interface Props {
  plan: PlanResponse;
  token: string | undefined;
  callBack: () => void;
  title: string;
}

export default function AlertDialogDeletePlan({
  plan,
  token,
  callBack,
  title,
}: Props) {
  return (
    <AlertDialogDelete
      callBack={callBack}
      model={plan}
      token={token}
      path="plans"
      title={title}
    />
  );
}
