import { Progress } from "@/components/ui/progress";
import { Check, X } from "lucide-react";
interface PasswordStrength {
  score: number;
  criteria: {
    minLength: boolean;
    hasLetter: boolean;
    hasNumber: boolean;
    hasSpecialChar: boolean;
  };
}

export function calculatePasswordStrength(password: string): PasswordStrength {
  const criteria = {
    minLength: password.length >= 8,
    hasLetter: /[a-zA-Z]/.test(password),
    hasNumber: /\d/.test(password),
    hasSpecialChar: /[!@#$%^&*(),.?":{}|<>]/.test(password),
  };

  const score = Object.values(criteria).filter(Boolean).length * 25;

  return { score, criteria };
}

export interface PasswordStrengthIndicatorTexts {
  minLength: string;
  hasLetter: string;
  hasNumber: string;
  hasSpecialChar: string;
  strength: string;
  weak: string;
  medium: string;
  strong: string;
}

interface PasswordStrengthIndicatorProps {
  password: string;
  texts: PasswordStrengthIndicatorTexts;
}
export function PasswordStrengthIndicator({
  password,
  texts,
}: PasswordStrengthIndicatorProps) {
  const strength = calculatePasswordStrength(password);

  const getColorClass = (score: number) => {
    if (score < 50) return "bg-destructive";
    if (score < 75) return "bg-yellow-500";
    return "bg-success";
  };

  const criteriaList = [
    { key: "minLength", label: texts.minLength },
    { key: "hasLetter", label: texts.hasLetter },
    { key: "hasNumber", label: texts.hasNumber },
    { key: "hasSpecialChar", label: texts.hasSpecialChar },
  ] as const;

  return (
    <div className="w-full space-y-2">
      <Progress
        value={strength.score}
        className={`w-full ${getColorClass(strength.score)} h-2`}
      />
      <p className={"text-sm text-gray-600"}>
        {texts.strength}
        {strength.score < 50
          ? texts.weak
          : strength.score < 75
            ? texts.medium
            : texts.strong}
      </p>
      <ul className="space-y-1">
        {criteriaList.map(({ key, label }) => (
          <li key={key} className="flex items-center space-x-2">
            {strength.criteria[key] ? (
              <>
                <Check className="w-4 h-4 text-success" />
                <span className="text-sm text-success">{label}</span>
              </>
            ) : (
              <>
                <X className="w-4 h-4 text-destructive" />
                <span className="text-sm text-gray-600">{label}</span>
              </>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}
