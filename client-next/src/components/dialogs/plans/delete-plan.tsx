import { AlertDialogDelete } from "@/components/dialogs/delete-model";
import { PlanResponse } from "@/types/dto";
import { forwardRef } from "react";

interface Props {
  plan: PlanResponse;
  token: string | undefined;
  callBack: () => void;
  title: string;
}

const AlertDialogDeletePlan = forwardRef<HTMLDivElement, Props>(
  ({ plan, token, callBack, title }, ref) => {
    return (
      <div ref={ref}>
        <AlertDialogDelete
          callBack={callBack}
          model={plan}
          token={token}
          path="plans"
          title={title}
        />
      </div>
    );
  },
);

AlertDialogDeletePlan.displayName = "AlertDialogDeletePlan";
export default AlertDialogDeletePlan;
