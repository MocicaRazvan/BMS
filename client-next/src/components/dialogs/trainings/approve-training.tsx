import { AlertDialogApprove } from "@/components/dialogs/approve-model";

import { TrainingResponse } from "@/types/dto";

interface Props {
  training: TrainingResponse;
  token: string | undefined;
  callBack: () => void;
}

export default function AlertDialogApproveTraining({
  training,
  token,
  callBack,
}: Props) {
  return (
    <AlertDialogApprove
      callBack={callBack}
      model={training}
      token={token}
      path="trainings"
      title="training"
    />
  );
}
