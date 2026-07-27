"use client";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import type { RegisteredBillingKey } from "@/types/billing";
import { cn } from "@/utils/cn";

interface BillingMethodListProps {
  methods: RegisteredBillingKey[];
  selectedBillingKey: string | null;
  onSelect: (billingKey: string) => void;
  onSetDefault: (billingKey: string) => void;
  disabled?: boolean;
  isUpdatingDefault?: boolean;
}

/**
 * 등록된 결제수단 목록.
 * 기본 결제수단 표시/변경과 선택 상태를 함께 보여준다.
 */
export function BillingMethodList({
  methods,
  selectedBillingKey,
  onSelect,
  onSetDefault,
  disabled = false,
  isUpdatingDefault = false,
}: BillingMethodListProps) {
  if (methods.length === 0) {
    return (
      <p className="rounded-lg border border-dashed p-4 text-sm text-muted-foreground">
        등록된 결제수단이 없습니다. 먼저 결제수단을 등록해주세요.
      </p>
    );
  }

  return (
    <ul className="space-y-2">
      {methods.map((method) => {
        const selected = method.billingKey === selectedBillingKey;

        return (
          <li key={method.billingKey}>
            <div
              className={cn(
                "rounded-lg border px-4 py-3 transition-colors",
                selected ? "border-primary bg-primary/5" : "border-border",
                disabled && "opacity-60"
              )}
            >
              <button
                type="button"
                disabled={disabled}
                onClick={() => onSelect(method.billingKey)}
                className="flex w-full items-start justify-between gap-3 text-left"
              >
                <div className="space-y-1">
                  <div className="flex flex-wrap items-center gap-2">
                    <p className="text-sm font-medium text-foreground">{method.displayName}</p>
                    {method.defaultMethod ? <Badge variant="success">기본</Badge> : null}
                  </div>
                  <p className="text-xs text-muted-foreground">{method.maskedCardNumber}</p>
                </div>
                <span
                  className={cn(
                    "mt-1 h-4 w-4 shrink-0 rounded-full border",
                    selected ? "border-primary bg-primary" : "border-muted-foreground"
                  )}
                  aria-hidden
                />
              </button>

              {!method.defaultMethod ? (
                <div className="mt-3">
                  <Button
                    type="button"
                    size="sm"
                    variant="outline"
                    disabled={disabled || isUpdatingDefault}
                    onClick={() => onSetDefault(method.billingKey)}
                  >
                    기본으로 설정
                  </Button>
                </div>
              ) : null}
            </div>
          </li>
        );
      })}
    </ul>
  );
}
