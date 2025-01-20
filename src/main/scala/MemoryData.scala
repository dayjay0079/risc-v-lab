import chisel3._
import chisel3.util._

class MemoryData(mem_size: Int) extends Module {
  val io = IO(new Bundle {
    val address = Input(UInt(32.W))
    val data_in = Input(SInt(8.W))
    val write_enable = Input(Bool())
    val data_out = Output(SInt(8.W))
  })

  val memory = SyncReadMem(mem_size >> 2, SInt(8.W)) // Type: SyncReadMem

  // Reading from memory
  io.data_out := memory.read((io.address >> 2.U).asUInt)

  // Writing to memory
  when(io.write_enable) {
    memory.write((io.address >> 2.U).asUInt, io.data_in)
  }
}
