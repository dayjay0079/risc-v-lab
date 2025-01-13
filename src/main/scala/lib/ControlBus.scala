package lib
import chisel3._

class ControlBus extends Bundle {
  val opcode = UInt(7.W)
  val funct3 = UInt(3.W)
  val funct7 = UInt(7.W)
  val inst_type = UInt(5.W)
  val write_enable_reg = Bool()
  val write_enable_mem = UInt(2.W)
  val mem_to_reg = Bool()
}
