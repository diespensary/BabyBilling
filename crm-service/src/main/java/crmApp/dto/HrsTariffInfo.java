package crmApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HrsTariffInfo {
    private Long id;

    String name;

    boolean isActive;

    LocalDateTime creationDate;

    String description;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class TariffParameterEntity {
        private Long id;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public class TariffTypeEntity {

            private Long id;

            String name;
        }


        private Double initiatingInternalCallCost;

        private Double receivingInternalCallCost;

        private Double initiatingExternalCallCost;

        private Double receivingExternalCallCost;

        private Long monthlyMinuteCapacity;

        private Double monthlyFee;
    }
}
