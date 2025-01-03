import { ArchiveQueue, QueueInformation } from "@/types/dto";
import useFetchStream from "@/hoooks/useFetchStream";

interface Args {
  queueName: ArchiveQueue;
  refresh?: boolean;
}

export default function useGetQueueArchive({
  queueName,
  refresh = false,
}: Args) {
  return useFetchStream<QueueInformation>({
    path: "/archive/queue",
    queryParams: {
      refresh: `${refresh}`,
      queueName,
    },
  });
}
