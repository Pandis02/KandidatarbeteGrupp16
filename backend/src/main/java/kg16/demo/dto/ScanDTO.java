package kg16.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScanDTO {

    @NotBlank
    private String hostname;

    @NotBlank
    private String ipAddress;

    @NotBlank
    private String macAddress;

    @NotNull
    private Integer status;

    private Timestamp lastSeen;
}
