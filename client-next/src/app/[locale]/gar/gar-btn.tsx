"use client";
import { Button } from "@/components/ui/button";
import { CustomEntityModel, DayBodyWithMeals, DayResponse } from "@/types/dto";
import { fetchStream } from "@/hoooks/fetchStream";
import { WithUser } from "@/lib/user";

interface Props extends WithUser {}

export default function GarBtn({ authUser }: Props) {
  const createDay = async () => {
    const body: DayBodyWithMeals = {
      body: "body",
      title: "title",
      type: "HIGH_CARB",
      meals: [
        {
          period: "11:30",
          recipes: [1, 3],
        },
        {
          period: "12:30",
          recipes: [1, 4],
        },
      ],
    };
    const res = await fetchStream<CustomEntityModel<DayResponse>>({
      path: "/days/create/meals",
      body,
      token: authUser.token,
      method: "POST",
    });

    console.log("DAY RES", res);
  };

  const getDay = async () => {
    const res = await fetchStream<CustomEntityModel<DayResponse>>({
      path: "/days/1",
      token: authUser.token,
      method: "GET",
    });

    console.log("DAY RES", res);
  };

  return <Button onClick={() => createDay()}>Gar</Button>;
}
