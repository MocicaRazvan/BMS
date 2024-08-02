import { AlertDialogDelete } from "@/components/dialogs/delete-model";
import { TrainingResponse } from "@/types/dto";

interface Props {
  training: TrainingResponse;
  token: string | undefined;
  callBack: () => void;
}

export default function AlertDialogDeleteTraining({
  training,
  token,
  callBack,
}: Props) {
  return (
    <AlertDialogDelete
      callBack={callBack}
      model={training}
      token={token}
      path="trainings"
      title="training"
    />
  );
}
