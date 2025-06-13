import { DayResponse } from "@/types/dto";
import { AlertDialogDelete } from "@/components/dialogs/delete-model";
import { forwardRef } from "react";

interface Props {
  day: DayResponse;
  token: string;
  callBack: () => void;
}

const AlertDialogDeleteDay = forwardRef<HTMLDivElement, Props>(
  ({ day, token, callBack }, ref) => {
    return (
      <div ref={ref}>
        <AlertDialogDelete
          model={day}
          token={token}
          path="days"
          title={day.title}
          callBack={callBack}
        />
      </div>
    );
  },
);
AlertDialogDeleteDay.displayName = "AlertDialogDeleteDay";
export default AlertDialogDeleteDay;
