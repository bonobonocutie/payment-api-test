"use client";

import { Loader2 } from "lucide-react";

import { Button } from "@/components/ui/button";

interface PaymentButtonProps {
  label?: string;
  loading?: boolean;
  disabled?: boolean;
  onClick: () => void;
}

/**
 * 재사용 가능한 결제 버튼.
 * 로딩/비활성 상태만 표현하고 결제 로직은 외부에서 주입받는다.
 */
export function PaymentButton({
  label = "테스트 결제",
  loading = false,
  disabled = false,
  onClick,
}: PaymentButtonProps) {
  return (
    <Button
      size="lg"
      className="w-full"
      disabled={disabled || loading}
      onClick={onClick}
      aria-busy={loading}
    >
      {loading ? (
        <>
          <Loader2 className="h-4 w-4 animate-spin" />
          처리 중...
        </>
      ) : (
        label
      )}
    </Button>
  );
}
