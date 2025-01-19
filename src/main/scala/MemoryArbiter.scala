import chisel3._
import chisel3.util._

class MemoryArbiter extends Module {
  val io = IO(new Bundle{
    val address_in = Input(SInt(32.W))
    val address_out = Output(UInt(32.W))
    val valid_mem = Output(Bool())
    val valid_led = Output(Bool())
    val valid_uart = Output(Bool())
    val valid_switches = Output(Bool())
    val valid_buttons = Output(Bool())
  })

  io.address_out := io.address_in(9, 0)

  io.valid_mem := io.address_in >= 0.S && io.address_in < 1024.S
  io.valid_led := io.address_in === 1024.S
  io.valid_switches := io.address_in === 1025.S
  io.valid_buttons := io.address_in === 1026.S
  io.valid_uart := io.address_in >= 2048.S && io.address_in < 2052.S
}
