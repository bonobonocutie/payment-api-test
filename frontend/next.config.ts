import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Docker 이미지에서 최소 런타임만 포함하기 위해 standalone 빌드를 사용한다.
  output: "standalone",
};

export default nextConfig;
