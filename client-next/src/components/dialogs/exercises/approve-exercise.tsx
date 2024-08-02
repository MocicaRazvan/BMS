import { AlertDialogApprove } from "@/components/dialogs/approve-model";

import { ExerciseResponse } from "@/types/dto";

interface Props {
  exercise: ExerciseResponse;
  token: string | undefined;
  callBack: () => void;
}

export default function AlertDialogApproveExercise({
  exercise,
  token,
  callBack,
}: Props) {
  return (
    <AlertDialogApprove
      callBack={callBack}
      model={exercise}
      token={token}
      path="exercises"
      title="exercise"
    />
  );
}
