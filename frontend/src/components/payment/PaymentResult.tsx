import { Badge } from "@/components/ui/badge";
import type { PaymentResultViewModel } from "@/types/payment";
import { PAYMENT_RESULT_STATUS } from "@/utils/constants";

interface PaymentResultProps {
  result: PaymentResultViewModel;
}

function resolveBadgeVariant(status: PaymentResultViewModel["status"]) {
  switch (status) {
    case PAYMENT_RESULT_STATUS.SUCCESS:
      return "success" as const;
    case PAYMENT_RESULT_STATUS.FAILURE:
      return "danger" as const;
    case PAYMENT_RESULT_STATUS.CANCELLED:
      return "warning" as const;
    case PAYMENT_RESULT_STATUS.LOADING:
      return "secondary" as const;
    default:
      return "outline" as const;
  }
}

export function PaymentResult({ result }: PaymentResultProps) {
  return (
    <div className="rounded-lg border bg-muted/30 p-4">
      <div className="mb-3 flex items-center justify-between gap-3">
        <h2 className="text-sm font-semibold text-foreground">결제 결과</h2>
        <Badge variant={resolveBadgeVariant(result.status)}>{result.status}</Badge>
      </div>
      <p className="text-base font-medium text-foreground">{result.title}</p>
      <p className="mt-1 text-sm text-muted-foreground">{result.description}</p>
      {result.paymentId ? (
        <p className="mt-3 break-all text-xs text-muted-foreground">
          paymentId: {result.paymentId}
        </p>
      ) : null}
    </div>
  );
}
