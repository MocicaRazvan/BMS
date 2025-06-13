import { AlertDialogApprove } from "@/components/dialogs/approve-model";

import { PlanResponse } from "@/types/dto";
import { WithUser } from "@/lib/user";
import { approvePlanNotification } from "@/context/plan-approve-notification-context";
import { forwardRef } from "react";

interface Props extends WithUser {
  plan: PlanResponse;
  callBack: () => void;
}

const AlertDialogApprovePlan = forwardRef<HTMLDivElement, Props>(
  ({ plan, authUser, callBack }, ref) => {
    return (
      <div ref={ref}>
        <AlertDialogApprove
          callBack={callBack}
          model={plan}
          authUser={authUser}
          path="plans"
          title={plan.title}
          approved={!plan.approved}
          notificationName={approvePlanNotification}
          stompExtraLink={`/trainer/plans/single/${plan.id}`}
        />
      </div>
    );
  },
);
AlertDialogApprovePlan.displayName = "AlertDialogApprovePlan";

export default AlertDialogApprovePlan;
