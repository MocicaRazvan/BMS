import { AlertDialogApprove } from "@/components/dialogs/approve-model";

import { PlanResponse } from "@/types/dto";
import { WithUser } from "@/lib/user";
import { approvePostNotificationName } from "@/context/post-approve-notification-context";
import { approvePlanNotification } from "@/context/plan-approve-notification-context";

interface Props extends WithUser {
  plan: PlanResponse;
  callBack: () => void;
}

export default function AlertDialogApprovePlan({
  plan,
  authUser,
  callBack,
}: Props) {
  return (
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
  );
}
