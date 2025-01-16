import chisel3._
import chisel3.util._

class MemoryArbiter extends Module {
  val io = IO(new Bundle{
    val address_in = Input(UInt(32.W))
    val address_out = Output(UInt(32.W))
    val valid_mem = Output(Bool())
    val valid_led = Output(Bool())
    val valid_uart = Output(Bool())
  })

  io.address_out := io.address_in(9, 0)

  io.valid_mem := !(io.address_in(10) || io.address_in(11))
  io.valid_led := io.address_in(10)
  io.valid_uart := io.address_in(11)
}
