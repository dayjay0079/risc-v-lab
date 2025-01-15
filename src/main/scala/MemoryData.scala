import chisel3._
import chisel3.util._

class MemoryData(fpga: Boolean, mem_size: Int) extends Module {
  val io = IO(new Bundle {
    val address = Input(UInt(32.W))
    val data_in = Input(SInt(8.W))
    val write_enable = Input(Bool())
    val data_out = Output(SInt(8.W))
  })

  // Define a method to handle unified memory access
  def readMemory(addr: UInt): SInt = {
    if (fpga) {
      memory.asInstanceOf[SyncReadMem[SInt]].read((addr >> 2.U).asUInt)
    } else {
      memory.asInstanceOf[Vec[SInt]]((addr >> 2.U).asUInt)
    }
  }
  def writeMemory(addr: UInt, data: SInt) = {
    if (fpga) {
      memory.asInstanceOf[SyncReadMem[SInt]].write((addr >> 2.U).asUInt, data)
    } else {
      memory.asInstanceOf[Vec[SInt]]((addr >> 2.U).asUInt) := data
    }
  }

  // Declare memory as AnyRef (parent type for both SyncReadMem and Vec)
  private val memory: AnyRef = if (fpga) {
    SyncReadMem(mem_size >> 2, SInt(8.W)) // Type: SyncReadMem
  } else {
    RegInit(VecInit(Seq.fill(mem_size >> 2)(0.S(8.W)))) // Type: Vec
  }

  // Reading from memory
  if (fpga) {
    io.data_out := readMemory(io.address)
  } else {
    io.data_out := RegNext(readMemory(io.address))
  }

  // Writing to memory
  when(io.write_enable) {
    writeMemory(io.address, io.data_in)
  }
}
