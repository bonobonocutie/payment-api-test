"use client";

import { BillingMethodList } from "@/components/payment/BillingMethodList";
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
import { Button } from "@/components/ui/button";
import { useBillingKeys } from "@/hooks/useBillingKeys";
import { useBillingPayment } from "@/hooks/useBillingPayment";
import { usePayment } from "@/hooks/usePayment";

/**
 * 테스트 결제 확인용 페이지.
 * 1) 일회성 테스트 결제
 * 2) 카드 여러 장 등록 / 기본수단 지정 / 기본수단 결제
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

  const {
    methods,
    selectedBillingKey,
    defaultBillingKey,
    isLoading: isBillingLoading,
    isRegistering,
    isUpdatingDefault,
    error: billingError,
    result: billingRegisterResult,
    selectBillingKey,
    reloadMethods,
    registerPaymentMethod,
    setAsDefault,
  } = useBillingKeys();

  const {
    isPaying: isBillingPaying,
    result: billingPayResult,
    payWithSelectedMethod,
  } = useBillingPayment();

  const defaultMethod = methods.find((method) => method.billingKey === defaultBillingKey);

  return (
    <main className="mx-auto flex min-h-screen w-full max-w-xl flex-col gap-6 px-4 py-10">
      <Card className="w-full">
        <CardHeader>
          <CardTitle>PortOne 테스트 결제</CardTitle>
          <CardDescription>
            일회성 결제창으로 테스트 결제가 동작하는지 확인합니다.
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
            label="테스트 결제"
            loading={isPaying}
            disabled={isReadyLoading || !readyInfo}
            onClick={() => {
              void startTestPayment();
            }}
          />

          <PaymentResult result={result} />
        </CardContent>
      </Card>

      <Card className="w-full">
        <CardHeader>
          <CardTitle>등록 결제수단</CardTitle>
          <CardDescription>
            카드를 여러 장 등록하고, 기본 결제수단을 지정한 뒤 그 카드로 결제합니다.
          </CardDescription>
        </CardHeader>

        <CardContent className="space-y-6">
          {isBillingLoading ? (
            <p className="text-sm text-muted-foreground">등록된 결제수단을 불러오는 중...</p>
          ) : null}

          {!isBillingLoading && billingError ? (
            <div className="space-y-3 rounded-lg border border-destructive/30 bg-destructive/5 p-4">
              <p className="text-sm text-destructive">{billingError}</p>
              <Button variant="outline" onClick={() => void reloadMethods()}>
                다시 불러오기
              </Button>
            </div>
          ) : null}

          {defaultMethod ? (
            <p className="text-sm text-muted-foreground">
              현재 기본 결제수단:{" "}
              <span className="font-medium text-foreground">
                {defaultMethod.displayName} ({defaultMethod.maskedCardNumber})
              </span>
            </p>
          ) : (
            <p className="text-sm text-muted-foreground">기본 결제수단이 아직 없습니다.</p>
          )}

          <BillingMethodList
            methods={methods}
            selectedBillingKey={selectedBillingKey}
            onSelect={selectBillingKey}
            onSetDefault={(billingKey) => {
              void setAsDefault(billingKey);
            }}
            disabled={isRegistering || isBillingPaying || isUpdatingDefault}
            isUpdatingDefault={isUpdatingDefault}
          />

          <div className="grid gap-3 sm:grid-cols-2">
            <PaymentButton
              label="결제수단 등록"
              loading={isRegistering}
              disabled={isBillingPaying || isUpdatingDefault}
              onClick={() => {
                void registerPaymentMethod();
              }}
            />
            <PaymentButton
              label="기본 수단으로 결제"
              loading={isBillingPaying}
              disabled={isRegistering || isUpdatingDefault || !defaultBillingKey}
              onClick={() => {
                void payWithSelectedMethod(defaultBillingKey);
              }}
            />
          </div>

          <PaymentResult result={billingRegisterResult} />
          <PaymentResult result={billingPayResult} />
        </CardContent>
      </Card>
    </main>
  );
}
