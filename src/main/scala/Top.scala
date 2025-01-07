import chisel3._
import chisel3.util._

import lib.peripherals.{MemoryMappedUart, StringStreamer}
import lib.peripherals.MemoryMappedUart.UartPins

object Top extends App {
  emitVerilog(
    new Top(50000000, 9600),
    Array("--target-dir", "generated")
  )
}

class Top(freq: Int, baud: Int) extends Module {

}
