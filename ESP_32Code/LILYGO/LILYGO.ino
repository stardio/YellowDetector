#include <ModbusRTU.h>

#define RS485_RX 21
#define RS485_TX 22
#define RS485_RE_PIN 17       
#define RS485_SHUTDOWN_PIN 19 
#define BOOST_ENABLE_PIN 16   
#define SLAVE_ID 1

ModbusRTU mb;
uint16_t RAW_R = 0, RAW_G = 0, RAW_B = 0;

void setup() {
    Serial.begin(115200);

    pinMode(BOOST_ENABLE_PIN, OUTPUT);
    digitalWrite(BOOST_ENABLE_PIN, HIGH);
    pinMode(RS485_SHUTDOWN_PIN, OUTPUT);
    digitalWrite(RS485_SHUTDOWN_PIN, HIGH);
    
    // RE/DE 핀 설정: ModbusRTU 라이브러리가 자동으로 제어하도록 설정
    pinMode(RS485_RE_PIN, OUTPUT);
    digitalWrite(RS485_RE_PIN, LOW);

    Serial1.begin(115200, SERIAL_8N1, RS485_RX, RS485_TX);
    
    // 라이브러리에 RE_PIN 번호를 알려주어 송수신 전환을 자동으로 맡깁니다.
    mb.begin(&Serial1, RS485_RE_PIN); 
    mb.slave(SLAVE_ID);
    
    // 주소 등록: PLC가 257, 258, 259를 찾으므로 명확하게 해당 번호를 등록합니다.
    mb.addIreg(257, 0); // Blue
    mb.addIreg(258, 0); // Red
    mb.addIreg(259, 0); // Green

    Serial.println("\n--- PLC Address Link Success ---");
    Serial.println("R,G,B 형식으로 입력하면 PLC로 전달됩니다.");
}

void loop() {
    mb.task(); // PLC 응답 처리 (이제 84 에러 대신 정상 데이터를 보낼 것입니다)

    if (Serial.available()) {
        String input = Serial.readStringUntil('\n');
        input.trim();
        int first = input.indexOf(',');
        int second = input.lastIndexOf(',');

        if (first != -1 && second != -1) {
            RAW_R = input.substring(0, first).toInt();
            RAW_G = input.substring(first + 1, second).toInt();
            RAW_B = input.substring(second + 1).toInt();

            // PLC 레지스터에 값 세팅
            mb.Ireg(257, RAW_B); 
            mb.Ireg(258, RAW_R);
            mb.Ireg(259, RAW_G);
            
            Serial.printf("[수동입력] PLC전송 -> R:%d G:%d B:%d\n", RAW_R, RAW_G, RAW_B);
        }
    }
    yield();
}