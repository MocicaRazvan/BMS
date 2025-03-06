import { AuthProvider, Role } from "./fetch-utils";
import { Message } from "ai/react";

export interface ImagesDto {
  images: string[];
}

export interface VideosDto {
  videos: string[];
}

export interface IdGenerateDto {
  id: number;
  createdAt: string;
  updatedAt: string;
}

export interface WithUserId {
  userId: number;
}

export interface WithUserDto extends IdGenerateDto {
  userId: number;
}

export interface TitleBodyDto {
  title: string;
  body: string;
}

export interface TitleBodyImagesDto extends TitleBodyDto, ImagesDto {}

export interface TitleBodyUserDto extends WithUserDto, TitleBodyDto {
  userLikes: number[];
  userDislikes: number[];
}

export interface TitleBodyImagesUserDto extends TitleBodyUserDto, ImagesDto {}

export interface ApproveDto extends TitleBodyImagesUserDto {
  approved: boolean;
}

export interface PageInfo {
  currentPage: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
}

export interface PageableResponse<T> {
  content: T;
  pageInfo: PageInfo;
  links: Record<string, string>[] | null;
}

export interface ResponseWithChildList<E, C> {
  entity: E;
  children: C[];
}

export interface CustomEntityModel<T> {
  content: T;
  _links?: Record<string, string>[] | null;
}

export interface ResponseWithChildListEntity<E, C> {
  entity: CustomEntityModel<E>;
  children: C[];
}

export interface UserDto extends IdGenerateDto {
  firstName: string;
  lastName: string;
  email: string;
  role: Role;
  image: string;
  provider: AuthProvider;
  emailVerified: boolean;
}

export interface ResponseWithUserDto<T> {
  model: T;
  user: UserDto;
}

export interface ResponseWithUserDtoEntity<T> {
  model: CustomEntityModel<T>;
  user: UserDto;
}

export interface ResponseWithUserLikesAndDislikes<T>
  extends ResponseWithUserDto<T> {
  userLikes: UserDto[];
  userDislikes: UserDto[];
}

export interface ResponseWithUserLikesAndDislikesEntity<T>
  extends ResponseWithUserDtoEntity<T> {
  userLikes: UserDto[];
  userDislikes: UserDto[];
}

export interface ResponseWithEntityCount<T> {
  model: T;
  count: number;
}

export interface PageableBody {
  page: number;
  size: number;
  sortingCriteria?: Record<string, string>;
}

export interface UserBody {
  firstName: string;
  lastName: string;
}

export interface PostBody extends TitleBodyDto {
  tags: string[];
}

export interface PostResponse extends ApproveDto {
  tags: string[];
}

export interface CommentBody extends TitleBodyDto {}

export interface CommentResponse extends TitleBodyUserDto {
  referenceId: number;
}

export type ConnectedStatus = "ONLINE" | "OFFLINE";

export interface ConversationUserBase extends IdGenerateDto {
  email: string;
  connectedStatus: ConnectedStatus;
}

export interface ConversationUserPayload extends ConversationUserBase {
  connectedChatRoomId?: number;
}

export interface ConnectUserPayload {
  connectedStatus: ConnectedStatus;
}

export interface ChatRoomPayload {
  users: ConversationUserBase[];
}

export interface ChatRoomResponse extends IdGenerateDto {
  users: ConversationUserResponse[];
}

export interface ConversationUserResponse extends ConversationUserBase {
  connectedChatRoom?: ChatRoomResponse;
}

export interface ChatMessagePayload {
  senderEmail: string;
  receiverEmail: string;
  chatRoomId: number;
  content: string;
}

export interface ChatMessageResponse {
  id: number;
  sender: ConversationUserResponse;
  receiver: ConversationUserResponse;
  chatRoom: ChatRoomResponse;
  content: string;
  timestamp: string;
}

export interface ChatRoomUserDto {
  chatId: number;
  userEmail: string;
}

export interface NotificationTemplateBody<E extends string> {
  senderEmail: string;
  receiverEmail: string;
  type?: E;
  referenceId: number;
  content: string;
  extraLink?: string;
}

export type ChatMessageNotificationType = "NEW_MESSAGE";

export interface ChatMessageNotificationBody
  extends NotificationTemplateBody<ChatMessageNotificationType> {}

export interface NotificationTemplateResponse<
  R extends IdGenerateDto,
  E extends string,
> extends IdGenerateDto {
  sender: ConversationUserResponse;
  receiver: ConversationUserResponse;
  type: E;
  reference: R;
  content: string;
  extraLink?: string;
  timestamp: string;
}

export interface ChatMessageNotificationResponse
  extends NotificationTemplateResponse<
    ChatRoomResponse,
    ChatMessageNotificationType
  > {}

export interface SenderTypeDto<E extends string> {
  senderEmail: string;
  type: E;
}

export type DietType = "VEGAN" | "VEGETARIAN" | "OMNIVORE";
// | "CARNIVORE" ;

export const dietTypes: DietType[] = ["VEGAN", "VEGETARIAN", "OMNIVORE"];

export type UnitType = "GRAM" | "MILLILITER";

export interface IngredientBody {
  name: string;
  type: DietType;
}

export interface NutritionalFactBody {
  fat: number;
  saturatedFat: number;
  carbohydrates: number;
  sugar: number;
  protein: number;
  salt: number;
  unit: UnitType;
}

export interface IngredientResponse extends WithUserDto, IngredientBody {
  display: boolean;
}

export interface NutritionalFactResponse
  extends WithUserDto,
    NutritionalFactBody {}

export interface IngredientNutritionalFactBody {
  ingredient: IngredientBody;
  nutritionalFact: NutritionalFactBody;
}

export interface IngredientNutritionalFactResponse {
  ingredient: IngredientResponse;
  nutritionalFact: NutritionalFactResponse;
}

export interface IngredientQuantityDto {
  ingredientId: number;
  quantity: number;
}

export interface RecipeBody extends TitleBodyDto {
  type: DietType;
  ingredients: IngredientQuantityDto[];
}

export interface RecipeResponse extends ApproveDto {
  type: DietType;
  videos: string[];
}
export interface IngredientNutritionalFactResponseWithCount
  extends IngredientNutritionalFactResponse {
  count: number;
}

export interface ComposeMealBody {
  recipes: number[];
  period: string;
}

export interface MealBody extends ComposeMealBody {
  dayId: number;
}

export interface MealResponse extends MealBody, IdGenerateDto {}

export type DayType =
  | "LOW_CARB"
  | "HIGH_CARB"
  | "HIGH_PROTEIN"
  | "LOW_FAT"
  | "HIGH_FAT"
  | "LOW_PROTEIN"
  | "BALANCED";

export const dayTypes: DayType[] = [
  "LOW_CARB",
  "HIGH_CARB",
  "HIGH_PROTEIN",
  "LOW_FAT",
  "HIGH_FAT",
  "LOW_PROTEIN",
  "BALANCED",
] as const;

export interface DayBody extends TitleBodyDto {
  type: DayType;
}

export interface DayResponse extends TitleBodyImagesUserDto {
  type: DayType;
}

export interface DayBodyWithMeals extends DayBody {
  meals: ComposeMealBody[];
}

//todo change to new Plan data type

export type ObjectiveType = "GAIN_MASS" | "LOSE_WEIGHT" | "MAINTAIN_WEIGHT";

export const planObjectives: ObjectiveType[] = [
  "GAIN_MASS",
  "LOSE_WEIGHT",
  "MAINTAIN_WEIGHT",
] as const;
export interface PlanBody extends TitleBodyDto {
  type: DietType;
  objective: ObjectiveType;
  price: number;
  days: number[];
}

export interface PlanResponse extends PlanBody, ApproveDto {
  display: boolean;
}

export interface CheckoutRequestBody {
  plans: PlanResponse[];
  total: number;
  locale: string;
}

export interface CustomAddressDto extends IdGenerateDto {
  city: string;
  country: string;
  line1: string;
  line2: string;
  postalCode: string;
  state: string;
}

export interface OrderDto extends WithUserDto {
  addressId: number;
  planIds: number[];
  total: number;
  stripeInvoiceId: string;
}

export interface OrderDtoWithAddress {
  address: CustomAddressDto;
  order: OrderDto;
}

export interface SessionResponse {
  url: string;
  sessionId: string;
}
export interface CustomInvoiceDto {
  number: string;
  currency: string;
  amount: number;
  url: string;
  creationDate: string;
}
export interface InvoiceResponse {
  invoices: CustomInvoiceDto[];
  nextPageHasItems: boolean;
}
export interface UserSubscriptionDto {
  planId: number;
}

export interface MonthlyEntityGroup<M> {
  entity: M;
  month: number;
  year: number;
}

export interface CountAmount {
  count: number;
  totalAmount: number;
}

export interface MonthYear {
  month: number;
  year: number;
}
export interface MonthlyOrderSummary extends CountAmount, MonthYear {}
export interface DailyOrderSummary extends MonthlyOrderSummary {
  day: number;
}
export interface AverageAmount {
  averageAmount: number;
}

export interface MonthlyOrderSummaryType
  extends MonthlyOrderSummary,
    AverageAmount {
  type: DietType;
}

export interface MonthlyOrderSummaryObjective
  extends MonthlyOrderSummary,
    AverageAmount {
  objective: ObjectiveType;
}

export interface MonthlyOrderSummaryObjectiveType
  extends MonthlyOrderSummaryType,
    MonthlyOrderSummaryObjective {}
export enum CountrySummaryType {
  COUNT = "COUNT",
  TOTAL_AMOUNT = "TOTAL_AMOUNT",
}
export interface CountryOrderSummary {
  id: string;
  value: number;
  maxGroupTotal: number;
}

export interface EmailRequest {
  recipientEmail: string;
  subject: string;
  content: string;
}

export type KanbanTaskType = "URGENT" | "NORMAL" | "LOW";

export interface KanbanColumnBody {
  title: string;
  orderIndex: number;
}

export interface KanbanColumnResponse extends WithUserDto, KanbanColumnBody {}

export interface KanbanTaskBody {
  content: string;
  columnId: number;
  type: KanbanTaskType;
  orderIndex: number;
}

export interface KanbanTaskResponse extends WithUserDto, KanbanTaskBody {}

export type ApprovedNotificationType = "APPROVED" | "DISAPPROVED";

export interface ApproveModelNotificationResponse extends IdGenerateDto {
  approved: boolean;
  receiver: ConversationUserResponse;
  appId: number;
}

export interface NotificationPostResponse
  extends ApproveModelNotificationResponse {}

export interface ApproveNotificationBody
  extends NotificationTemplateBody<ApprovedNotificationType> {}

export interface ApproveNotificationResponse<
  T extends ApproveModelNotificationResponse,
> extends NotificationTemplateResponse<T, ApprovedNotificationType> {}

export interface ApprovePostNotificationBody extends ApproveNotificationBody {}

export interface ApprovePostNotificationResponse
  extends ApproveNotificationResponse<NotificationPostResponse> {}
export interface NotificationRecipeResponse
  extends ApproveModelNotificationResponse {}
export interface ApproveRecipeNotificationBody
  extends ApproveNotificationBody {}

export interface ApproveRecipeNotificationResponse
  extends ApproveNotificationResponse<NotificationRecipeResponse> {}
export interface NotificationPlanResponse
  extends ApproveModelNotificationResponse {}
export interface ApprovePlanNotificationBody extends ApproveNotificationBody {}

export interface ApprovePlanNotificationResponse
  extends ApproveNotificationResponse<NotificationPlanResponse> {}

export type BoughtNotificationType = "NEW_BOUGHT";

export interface BoughtNotificationBody
  extends NotificationTemplateBody<BoughtNotificationType> {}

export interface BoughtNotificationResponse
  extends NotificationTemplateResponse<
    PlanResponse & {
      appId: number;
    },
    BoughtNotificationType
  > {}

interface BaseAiChatMessage {
  content: string;
  vercelId: string;
  role: Message["role"];
}
export interface AiChatMessagePayload extends BaseAiChatMessage {
  email: string;
}

export interface AiChatMessageResponse
  extends BaseAiChatMessage,
    IdGenerateDto {}

export enum ArchiveQueue {
  COMMENT_UPDATE_QUEUE = "comment-update-queue",
  COMMENT_DELETE_QUEUE = "comment-delete-queue",
  DAY_DELETE_QUEUE = "day-delete-queue",
  DAY_UPDATE_QUEUE = "day-update-queue",
  INGREDIENT_DELETE_QUEUE = "ingredient-delete-queue",
  INGREDIENT_UPDATE_QUEUE = "ingredient-update-queue",
  MEAL_DELETE_QUEUE = "meal-delete-queue",
  MEAL_UPDATE_QUEUE = "meal-update-queue",
  PLAN_DELETE_QUEUE = "plan-delete-queue",
  PLAN_UPDATE_QUEUE = "plan-update-queue",
  POST_DELETE_QUEUE = "post-delete-queue",
  POST_UPDATE_QUEUE = "post-update-queue",
  RECIPE_DELETE_QUEUE = "recipe-delete-queue",
  RECIPE_UPDATE_QUEUE = "recipe-update-queue",
  USER_DELETE_QUEUE = "user-delete-queue",
  USER_UPDATE_QUEUE = "user-update-queue",
}
export type ArchiveQueuePrefix =
  | "comment"
  | "day"
  | "ingredient"
  | "meal"
  | "plan"
  | "post"
  | "recipe"
  | "user";

export const archiveQueuePrefixes = [
  "comment",
  "day",
  "ingredient",
  "meal",
  "plan",
  "post",
  "recipe",
  "user",
] as const;
export interface QueueInformation {
  name: ArchiveQueue;
  messageCount: number;
  consumerCount: number;
  timestamp: string;
  cronExpression: string;
}
interface BaseWSDto {
  queueName: ArchiveQueue;
  id: string;
  timestamp: string;
}
export interface NotifyBatchUpdate extends BaseWSDto {
  numberProcessed: number;
  finished: boolean;
}
export enum ContainerAction {
  START_CRON = "START_CRON",
  START_MANUAL = "START_MANUAL",
  STOP = "STOP",
}

export interface NotifyContainerAction extends BaseWSDto {
  action: ContainerAction;
}

export interface RankSummary {
  rank: number;
}
export interface GroupSummary extends RankSummary {
  maxGroupTotal: number;
  minGroupTotal: number;
  avgGroupTotal: number;
}
export interface TotalAmountSummary {
  totalAmount: number;
}
export interface TopUsersSummary
  extends GroupSummary,
    WithUserId,
    TotalAmountSummary {
  ordersNumber: number;
  planValues: number[];
  plansNumber: number;
}

export interface TopTrainersSummary
  extends GroupSummary,
    WithUserId,
    TotalAmountSummary {
  planCount: number;
  averageAmount: number;
  maxGroupPlanCount: number;
  minGroupPlanCount: number;
  avgGroupPlanCount: number;
  typeCounts: Partial<Record<DietType, number>>;
  objectiveCounts: Partial<Record<ObjectiveType, number>>;
  typeAmounts: Partial<Record<DietType, number>>;
  objectiveAmounts: Partial<Record<ObjectiveType, number>>;
  typeAvgs: Partial<Record<DietType, number>>;
  objectiveAvgs: Partial<Record<ObjectiveType, number>>;
}

export interface TopPlansSummary extends RankSummary {
  planId: number;
  count: number;
  maxGroupCount: number;
  minGroupCount: number;
  avgGroupCount: number;
  ratio: number;
}

export interface Similarity {
  similarity: number;
}

export interface PostReposeWithSimilarity extends PostResponse, Similarity {}
export interface PlanReposeWithSimilarity extends PlanResponse, Similarity {}

export interface UserCartResponse extends WithUserDto {
  plans: PlanResponse[];
}

export interface UserCartBody {
  planIds: number[];
}

export interface MonthlyOrderSummaryPrediction extends MonthYear {
  countQuantiles: number[];
  totalAmountQuantiles: number[];
}

export interface OverallSummary {
  ordersCount: number;
  plansCount: number;
}
