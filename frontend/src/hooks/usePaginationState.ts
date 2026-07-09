import { useState } from "react";

export function usePaginationState(initialSize = 10) {
  const [page, setPage] = useState(0);
  const [size] = useState(initialSize);
  return { page, size, setPage };
}
