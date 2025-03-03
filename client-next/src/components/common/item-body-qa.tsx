import AnswerFromBodyForm, {
  AnswerFromBodyFormProps,
} from "@/components/forms/answer-from-body-form";
import ProseText from "@/components/common/prose-text";

interface Props {
  html: string | undefined;
  formProps: AnswerFromBodyFormProps;
}
export default function ItemBodyQa({ html, formProps }: Props) {
  return (
    <div className="w-full space-y-7">
      <ProseText html={html} />
      <AnswerFromBodyForm {...formProps} />
    </div>
  );
}
