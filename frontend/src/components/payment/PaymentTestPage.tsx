"use client";

import { PaymentButton } from "@/components/payment/PaymentButton";
import { PaymentResult } from "@/components/payment/PaymentResult";
import { PaymentSummary } from "@/components/payment/PaymentSummary";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { usePayment } from "@/hooks/usePayment";
import { Button } from "@/components/ui/button";

/**
 * 테스트 결제 확인용 페이지.
 * 표현만 담당하고 결제 흐름은 usePayment 훅에 위임한다.
 */
export function PaymentTestPage() {
  const {
    readyInfo,
    isReadyLoading,
    isPaying,
    readyError,
    result,
    startTestPayment,
    reloadReadyInfo,
  } = usePayment();

  return (
    <main className="mx-auto flex min-h-screen w-full max-w-xl items-center px-4 py-10">
      <Card className="w-full">
        <CardHeader>
          <CardTitle>PortOne 테스트 결제</CardTitle>
          <CardDescription>
            테스트 결제가 정상적으로 동작하는지 확인하는 페이지입니다.
          </CardDescription>
        </CardHeader>

        <CardContent className="space-y-6">
          {isReadyLoading ? (
            <p className="text-sm text-muted-foreground">결제 정보를 불러오는 중...</p>
          ) : null}

          {!isReadyLoading && readyError ? (
            <div className="space-y-3 rounded-lg border border-destructive/30 bg-destructive/5 p-4">
              <p className="text-sm text-destructive">{readyError}</p>
              <Button variant="outline" onClick={() => void reloadReadyInfo()}>
                다시 불러오기
              </Button>
            </div>
          ) : null}

          {!isReadyLoading && readyInfo ? (
            <PaymentSummary
              orderName={readyInfo.orderName}
              amount={readyInfo.amount}
              currency={readyInfo.currency}
              payMethod={readyInfo.payMethod}
            />
          ) : null}

          <PaymentButton
            loading={isPaying}
            disabled={isReadyLoading || !readyInfo}
            onClick={() => {
              void startTestPayment();
            }}
          />

          <PaymentResult result={result} />
        </CardContent>
      </Card>
    </main>
  );
}
