import { API_BASE_URL } from "@/utils/constants";
import { ApiError, type ApiResponse } from "@/types/api";

type HttpMethod = "GET" | "POST" | "PUT";

interface RequestOptions {
  method?: HttpMethod;
  body?: unknown;
  signal?: AbortSignal;
}

/**
 * fetch를 한 곳에서만 다루어 컴포넌트/서비스의 네트워크 결합도를 낮춘다.
 */
async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = "GET", body, signal } = options;

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers: {
      "Content-Type": "application/json",
      Accept: "application/json",
    },
    body: body === undefined ? undefined : JSON.stringify(body),
    signal,
    cache: "no-store",
  });

  let payload: ApiResponse<T>;

  try {
    payload = (await response.json()) as ApiResponse<T>;
  } catch {
    throw new ApiError("서버 응답을 해석할 수 없습니다.", "INVALID_RESPONSE", response.status);
  }

  if (!response.ok || !payload.success || payload.data === null) {
    throw new ApiError(
      payload.message || "요청 처리에 실패했습니다.",
      payload.code || "REQUEST_FAILED",
      response.status
    );
  }

  return payload.data;
}

export const httpClient = {
  get<T>(path: string, signal?: AbortSignal): Promise<T> {
    return request<T>(path, { method: "GET", signal });
  },
  post<T>(path: string, body: unknown, signal?: AbortSignal): Promise<T> {
    return request<T>(path, { method: "POST", body, signal });
  },
  put<T>(path: string, body: unknown, signal?: AbortSignal): Promise<T> {
    return request<T>(path, { method: "PUT", body, signal });
  },
};
