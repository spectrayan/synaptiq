class StreamBuffer:
    def __init__(self):
        self.buffer = ""
        self.is_suppressing = False

    def process_chunk(self, chunk: str) -> str:
        self.buffer += chunk
        to_yield = ""

        if not self.is_suppressing:
            # We look for either ```component or ```json
            idx_comp = self.buffer.find("```component")
            idx_json = self.buffer.find("```json")
            
            # Find the earliest occurrence
            idx = -1
            if idx_comp != -1 and idx_json != -1:
                idx = min(idx_comp, idx_json)
            elif idx_comp != -1:
                idx = idx_comp
            elif idx_json != -1:
                idx = idx_json
                
            if idx != -1:
                to_yield = self.buffer[:idx]
                self.buffer = self.buffer[idx:]  # keep from ``` onwards
                self.is_suppressing = True
            elif "```" in self.buffer:
                # Wait for it to potentially become ```component
                pass
            else:
                if len(self.buffer) > 15:
                    to_yield = self.buffer[:-15]
                    self.buffer = self.buffer[-15:]
        
        if self.is_suppressing:
            # We are inside a component block.
            # Look for the closing ```
            # Since the buffer starts with ```component, we look for the next ``` after index 3
            next_fence = self.buffer.find("```", 3)
            if next_fence != -1:
                # Block closed!
                self.buffer = self.buffer[next_fence + 3:]
                self.is_suppressing = False
                
                # Now the remaining buffer might be normal text. We don't yield it immediately
                # because we need to process it through the normal logic on the next chunk, 
                # but we can just recursively process it.
                remaining = self.buffer
                self.buffer = ""
                to_yield += self.process_chunk(remaining)

        return to_yield

    def flush(self) -> str:
        if not self.is_suppressing:
            to_yield = self.buffer
            self.buffer = ""
            return to_yield
        return ""
