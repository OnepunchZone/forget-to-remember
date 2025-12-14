package ru.sazon.forget_to_remember.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditState {
    private Long greetingId;
    private Long originalId;
    private boolean isPublicCopy;
    private String originalText;
}
