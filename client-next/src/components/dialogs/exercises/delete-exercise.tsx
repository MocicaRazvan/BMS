import { AlertDialogDelete } from "@/components/dialogs/delete-model";
import { ExerciseResponse } from "@/types/dto";

interface Props {
  exercise: ExerciseResponse;
  token: string | undefined;
  callBack: () => void;
}

export default function AlertDialogDeleteExercise({
  exercise,
  token,
  callBack,
}: Props) {
  return (
    <AlertDialogDelete
      callBack={callBack}
      model={exercise}
      token={token}
      path="exercises"
      title="exercise"
    />
  );
}
