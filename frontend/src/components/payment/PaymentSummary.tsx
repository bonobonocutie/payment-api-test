import { formatCurrency } from "@/utils/format";

interface PaymentSummaryProps {
  orderName: string;
  amount: number;
  currency: string;
  payMethod: string;
}

export function PaymentSummary({
  orderName,
  amount,
  currency,
  payMethod,
}: PaymentSummaryProps) {
  return (
    <dl className="space-y-3 text-sm">
      <div className="flex items-center justify-between gap-4">
        <dt className="text-muted-foreground">상품명</dt>
        <dd className="font-medium text-foreground">{orderName}</dd>
      </div>
      <div className="flex items-center justify-between gap-4">
        <dt className="text-muted-foreground">금액</dt>
        <dd className="text-lg font-semibold text-foreground">{formatCurrency(amount)}</dd>
      </div>
      <div className="flex items-center justify-between gap-4">
        <dt className="text-muted-foreground">통화</dt>
        <dd className="font-medium text-foreground">{currency}</dd>
      </div>
      <div className="flex items-center justify-between gap-4">
        <dt className="text-muted-foreground">결제수단</dt>
        <dd className="font-medium text-foreground">{payMethod}</dd>
      </div>
    </dl>
  );
}
