import { WithUser } from "@/lib/user";
import { PlanResponse } from "@/types/dto";
import ToggleDisplayDialog from "@/components/dialogs/toggle-display";
import { forwardRef } from "react";

interface Props extends WithUser {
  model: PlanResponse;
  callBack: () => void;
}
const ToggleDisplayPlan = forwardRef<HTMLDivElement, Props>(
  ({ callBack, model, authUser }, ref) => {
    return (
      <div ref={ref}>
        <ToggleDisplayDialog
          model={{ ...model, name: model.title }}
          path={`/plans/alterDisplay/${model.id}`}
          callBack={callBack}
          authUser={authUser}
        />
      </div>
    );
  },
);
ToggleDisplayPlan.displayName = "ToggleDisplayPlan";
export default ToggleDisplayPlan;
