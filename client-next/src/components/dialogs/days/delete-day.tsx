import { DayResponse } from "@/types/dto";
import { AlertDialogDelete } from "@/components/dialogs/delete-model";

interface Props {
  day: DayResponse;
  token: string;
  callBack: () => void;
}

export default function AlertDialogDeleteDay({ day, token, callBack }: Props) {
  return (
    <AlertDialogDelete
      model={day}
      token={token}
      path="days"
      title={day.title}
      callBack={callBack}
    />
  );
}
