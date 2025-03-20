package kg16.demo.model.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Recipient {
    private Long recipientId;
    private String recipientType;
    private String recipientValue;
    private List<Role> roles;
}
