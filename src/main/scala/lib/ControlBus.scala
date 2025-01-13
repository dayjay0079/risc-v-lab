package lib
import chisel3._

class ControlBus extends Bundle {
  val opcode = UInt(7.W)
  val funct3 = UInt(3.W)
  val funct7 = UInt(7.W)
  val inst_type = UInt(5.W)
}
